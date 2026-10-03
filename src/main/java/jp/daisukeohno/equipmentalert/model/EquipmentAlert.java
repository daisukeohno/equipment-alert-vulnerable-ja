package jp.daisukeohno.equipmentalert.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 設備アラート1件分の情報を保持するモデルクラス。
 * 工場内の各設備から送信されるアラート(異常発生通知)を表す。
 */
public class EquipmentAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private int alertId;
    private String equipmentCode;   // 設備コード(例: LINE-01-PRESS)
    private String alertLevel;      // アラートレベル(INFO / WARN / CRITICAL)
    private String message;         // アラート内容
    private Date occurredAt;        // 発生日時
    private String reportedBy;      // 報告者ID

    public EquipmentAlert() {
    }

    public EquipmentAlert(int alertId, String equipmentCode, String alertLevel,
                           String message, Date occurredAt, String reportedBy) {
        this.alertId = alertId;
        this.equipmentCode = equipmentCode;
        this.alertLevel = alertLevel;
        this.message = message;
        this.occurredAt = occurredAt;
        this.reportedBy = reportedBy;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Date occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    @Override
    public String toString() {
        return "EquipmentAlert{" +
                "alertId=" + alertId +
                ", equipmentCode='" + equipmentCode + '\'' +
                ", alertLevel='" + alertLevel + '\'' +
                ", message='" + message + '\'' +
                ", occurredAt=" + occurredAt +
                ", reportedBy='" + reportedBy + '\'' +
                '}';
    }
}
