-- ============================================================
-- work_news_day
-- ============================================================
CREATE TABLE IF NOT EXISTS work_news_day (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    news_date  DATE         NOT NULL,
    month_key  VARCHAR(7)   NOT NULL,
    status     INT          NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT,

    PRIMARY KEY (id),
    CONSTRAINT uq_work_news_day_date UNIQUE (news_date),
    INDEX idx_work_news_day_month_date (month_key, news_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- work_news_item
-- ============================================================
CREATE TABLE IF NOT EXISTS work_news_item (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    day_id                 BIGINT       NOT NULL,
    legacy_id              BIGINT,
    item_type              VARCHAR(40)  NOT NULL DEFAULT 'TASK',
    title                  VARCHAR(255),
    content                LONGTEXT     NOT NULL,
    meta_json              LONGTEXT,
    department_id          BIGINT       NOT NULL DEFAULT 0,
    position_id            BIGINT,
    assigned_position_name VARCHAR(255),
    deadline_date          DATE,
    fulfillment            LONGTEXT,
    sort_order             INT          NOT NULL DEFAULT 0,
    status                 INT          NOT NULL DEFAULT 1,
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             BIGINT,
    updated_at             DATETIME,
    updated_by             BIGINT,

    PRIMARY KEY (id),
    CONSTRAINT fk_work_news_item_day
        FOREIGN KEY (day_id) REFERENCES work_news_day (id)
        ON DELETE CASCADE,
    INDEX idx_work_news_item_day_sort       (day_id, sort_order),
    INDEX idx_work_news_item_legacy         (legacy_id),
    INDEX idx_work_news_item_department     (department_id),
    INDEX idx_work_news_item_day_department (day_id, department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
