-- APP_REGHUB_FCA_VALU_FACT_ALL
WITH RankedData AS (
    SELECT 
        v.VALU_RPT_UTI,
        v.VALU_KEYS_UITI,
        v.VALU_SUBMITTED_VALU_TS,
        v.VALU_state_status,
        v.VALU_state_sub_status,
        v.VALU_reason_codes,
        v.VALU_trade_id,
        v.rpt_trd_pty1_id AS valu_pty1,
        v.rpt_trd_pty2_id AS valu_pty2,
        vack.rpt_trd_pty1_id AS last_ack_valu_pty1,
        vack.rpt_trd_pty2_id AS last_ack_valu_pty2,
        vack.VALU_RPT_UTI AS last_ack_valu_trade_id,
        vack.VALU_SUBMITTED_VALU_TS AS last_ack_valu_ts,
        vack.VALU_SUBMITTED_VALU_TS_PREFEIT AS last_ack_valu_ts_prefeit,
        ROW_NUMBER() OVER (PARTITION BY v.VALU_KEYS_UITI ORDER BY v.VALU_SUBMITTED_VALU_TS DESC) AS rn
    FROM 
        GFOLYREG_WORK.APP_REGHUB_FCA_VALU_FACT V
    LEFT JOIN 
        GFOLYREG_WORK.APP_REGHUB_FCA_VALU_FACT_ACK VACK
        ON V.VALU_KEYS_UITI = VACK.VALU_KEYS_UITI
        AND V.VALU_trade_id = VACK.VALU_trade_id
        AND V.rpt_trd_pty1_id = VACK.rpt_trd_pty1_id
        AND V.rpt_trd_pty2_id = VACK.rpt_trd_pty2_id
)
SELECT 
    VALU_RPT_UTI,
    VALU_KEYS_UITI,
    VALU_SUBMITTED_VALU_TS,
    VALU_state_status,
    VALU_state_sub_status,
    VALU_reason_codes,
    VALU_trade_id,
    valu_pty1,
    valu_pty2,
    last_ack_valu_pty1,
    last_ack_valu_pty2,
    last_ack_valu_trade_id,
    last_ack_valu_ts,
    last_ack_valu_ts_prefeit
FROM RankedData
WHERE rn = 1;
