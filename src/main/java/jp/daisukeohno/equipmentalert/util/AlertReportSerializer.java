package jp.daisukeohno.equipmentalert.util;

import jp.daisukeohno.equipmentalert.model.EquipmentAlert;

import java.io.*;
import java.util.*;

/**
 * アラートレポートをファイルへシリアライズ/デシリアライズするユーティリティ。
 * 他拠点とのレポート受け渡しに使用する想定。
 * 許可リスト方式で復元クラスを EquipmentAlert と java.util.Date に制限し、
 * ガジェットチェーンを利用したデシリアライズ攻撃を防止する。
 */
public class AlertReportSerializer {

    private static final Set<String> ALLOWED_CLASSES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    EquipmentAlert.class.getName(),
                    Date.class.getName()
            ))
    );

    public void saveReport(EquipmentAlert alert, String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(alert);
        }
    }

    /**
     * 許可リスト方式で EquipmentAlert と java.util.Date 以外のクラスを拒否し、
     * ガジェットチェーンを利用したデシリアライズを防止する。
     */
    public EquipmentAlert loadReport(InputStream rawInput) throws IOException, ClassNotFoundException {
        try (AllowlistObjectInputStream in = new AllowlistObjectInputStream(rawInput)) {
            Object report = in.readObject();
            if (!(report instanceof EquipmentAlert)) {
                throw new InvalidClassException(report == null ? "null" : report.getClass().getName(),
                        "EquipmentAlert以外のクラスは復元できません");
            }
            return (EquipmentAlert) report;
        }
    }

    private static class AllowlistObjectInputStream extends ObjectInputStream {

        private AllowlistObjectInputStream(InputStream input) throws IOException {
            super(input);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            if (!ALLOWED_CLASSES.contains(desc.getName())) {
                throw new InvalidClassException(desc.getName(), "デシリアライズが許可されていないクラスです");
            }
            return super.resolveClass(desc);
        }

        @Override
        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            throw new InvalidClassException("動的プロキシのデシリアライズは許可されていません");
        }
    }
}
