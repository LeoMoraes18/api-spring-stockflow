-- ============================================================
-- V1 - Esquema inicial: cadastro, pedidos de venda e estoque
-- ============================================================

-- ------------------------------------------------------------
-- usuario
-- Base da autenticacao (Fase 5) e origem das colunas de auditoria.
-- Criada primeiro porque todas as demais referenciam created_by.
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    senha_hash    VARCHAR(60)  NOT NULL,
    ultimo_login  TIMESTAMPTZ  NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NULL
);

-- ------------------------------------------------------------
-- cliente
-- ------------------------------------------------------------
CREATE TABLE cliente (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(180) NOT NULL,
    documento   VARCHAR(14)  NULL,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  UUID         NULL REFERENCES usuario (id),
    updated_at  TIMESTAMPTZ  NULL,
    updated_by  UUID         NULL REFERENCES usuario (id),
    deleted_at  TIMESTAMPTZ  NULL,
    deleted_by  UUID         NULL REFERENCES usuario (id)
);

-- Unicidade apenas entre clientes ativos: um documento pode ser
-- recadastrado depois que o registro anterior sofreu soft delete.
CREATE UNIQUE INDEX ux_cliente_documento
    ON cliente (documento)
    WHERE deleted_at IS NULL AND documento IS NOT NULL;

-- ------------------------------------------------------------
-- produto
-- ------------------------------------------------------------
CREATE TABLE produto (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(180)  NOT NULL,
    descricao       TEXT          NULL,
    unidade_medida  VARCHAR(10)   NOT NULL,
    preco_venda     NUMERIC(15,2) NOT NULL DEFAULT 0,
    preco_compra    NUMERIC(15,2) NULL,

    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by      UUID          NULL REFERENCES usuario (id),
    updated_at      TIMESTAMPTZ   NULL,
    updated_by      UUID          NULL REFERENCES usuario (id),
    deleted_at      TIMESTAMPTZ   NULL,
    deleted_by      UUID          NULL REFERENCES usuario (id),

    CONSTRAINT ck_produto_preco_venda   CHECK (preco_venda >= 0),
    CONSTRAINT ck_produto_preco_compra  CHECK (preco_compra IS NULL OR preco_compra >= 0)
);

-- ------------------------------------------------------------
-- saldo_estoque
-- Saldo consolidado, uma linha por produto. Existe para ser a
-- LINHA TRAVAVEL do controle de concorrencia: 'version' e usada
-- pelo lock otimista (@Version) e o CHECK garante, no banco, que
-- nenhum caminho de codigo consiga vender a descoberto.
-- A verdade auditavel continua em movimento_estoque.
-- ------------------------------------------------------------
CREATE TABLE saldo_estoque (
    produto_id  UUID          PRIMARY KEY REFERENCES produto (id),
    quantidade  NUMERIC(15,3) NOT NULL DEFAULT 0,
    version     BIGINT        NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_saldo_estoque_nao_negativo CHECK (quantidade >= 0)
);

-- ------------------------------------------------------------
-- pedido
-- ------------------------------------------------------------
CREATE TABLE pedido (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido  VARCHAR(20)   NOT NULL UNIQUE,
    cliente_id     UUID          NOT NULL REFERENCES cliente (id),
    data           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    status         VARCHAR(20)   NOT NULL,
    valor_total    NUMERIC(15,2) NOT NULL DEFAULT 0,

    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by     UUID          NULL REFERENCES usuario (id),
    updated_at     TIMESTAMPTZ   NULL,
    updated_by     UUID          NULL REFERENCES usuario (id),
    deleted_at     TIMESTAMPTZ   NULL,
    deleted_by     UUID          NULL REFERENCES usuario (id),

    CONSTRAINT ck_pedido_status CHECK (status IN (
        'ABERTO', 'CONFIRMADO', 'PAGO', 'SEPARADO',
        'ENVIADO', 'ENTREGUE', 'CANCELADO'
    )),
    CONSTRAINT ck_pedido_valor_total CHECK (valor_total >= 0)
);

CREATE INDEX ix_pedido_cliente_id ON pedido (cliente_id);
CREATE INDEX ix_pedido_status     ON pedido (status);

-- ------------------------------------------------------------
-- item_pedido
-- unidade_medida e valor_unitario sao FOTOGRAFADOS do produto no
-- momento da venda: alteracao de cadastro nao pode reescrever o
-- historico de faturamento.
-- ------------------------------------------------------------
CREATE TABLE item_pedido (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id        UUID          NOT NULL REFERENCES pedido (id),
    produto_id       UUID          NOT NULL REFERENCES produto (id),
    unidade_medida   VARCHAR(10)   NOT NULL,
    quantidade       NUMERIC(15,3) NOT NULL,
    valor_unitario   NUMERIC(15,2) NOT NULL,
    valor_desconto   NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_acrescimo  NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_total      NUMERIC(15,2) NOT NULL,

    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by       UUID          NULL REFERENCES usuario (id),
    updated_at       TIMESTAMPTZ   NULL,
    updated_by       UUID          NULL REFERENCES usuario (id),
    deleted_at       TIMESTAMPTZ   NULL,
    deleted_by       UUID          NULL REFERENCES usuario (id),

    CONSTRAINT ck_item_pedido_quantidade      CHECK (quantidade > 0),
    CONSTRAINT ck_item_pedido_valor_unitario  CHECK (valor_unitario >= 0),
    CONSTRAINT ck_item_pedido_desconto        CHECK (valor_desconto >= 0),
    CONSTRAINT ck_item_pedido_acrescimo       CHECK (valor_acrescimo >= 0),
    CONSTRAINT ck_item_pedido_valor_total     CHECK (valor_total >= 0),

    -- Decisao de negocio: o mesmo produto nao se repete em duas
    -- linhas do mesmo pedido. Remova se quiser permitir.
    CONSTRAINT ux_item_pedido_produto UNIQUE (pedido_id, produto_id)
);

CREATE INDEX ix_item_pedido_pedido_id  ON item_pedido (pedido_id);
CREATE INDEX ix_item_pedido_produto_id ON item_pedido (produto_id);

-- ------------------------------------------------------------
-- movimento_estoque
-- Razao APPEND-ONLY: nao possui updated_at nem deleted_at de
-- proposito. Movimento errado nao se edita nem se apaga - estorna-se
-- com um movimento de sentido contrario.
-- item_pedido_id e NULL em entradas/ajustes que nao vem de venda.
-- ------------------------------------------------------------
CREATE TABLE movimento_estoque (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id      UUID          NOT NULL REFERENCES produto (id),
    item_pedido_id  UUID          NULL REFERENCES item_pedido (id),
    unidade_medida  VARCHAR(10)   NOT NULL,
    quantidade      NUMERIC(15,3) NOT NULL,
    tipo_movimento  VARCHAR(10)   NOT NULL,
    observacao      TEXT          NULL,

    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by      UUID          NULL REFERENCES usuario (id),

    CONSTRAINT ck_movimento_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_movimento_tipo       CHECK (tipo_movimento IN ('ENTRADA', 'SAIDA'))
);

CREATE INDEX ix_movimento_estoque_produto_id     ON movimento_estoque (produto_id);
CREATE INDEX ix_movimento_estoque_item_pedido_id ON movimento_estoque (item_pedido_id);

-- ------------------------------------------------------------
-- Documentacao das decisoes menos obvias
-- ------------------------------------------------------------
COMMENT ON TABLE  saldo_estoque              IS 'Saldo consolidado por produto. Linha travavel para lock otimista.';
COMMENT ON COLUMN saldo_estoque.version      IS 'Contador de versao usado pelo @Version do JPA (lock otimista).';
COMMENT ON TABLE  movimento_estoque          IS 'Razao append-only de movimentacao. Fonte auditavel do saldo.';
COMMENT ON COLUMN item_pedido.valor_unitario IS 'Preco fotografado no momento da venda.';
