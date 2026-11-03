package jp.daisukeohno.equipmentalert.util;

import jp.daisukeohno.equipmentalert.model.EquipmentAlert;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class AlertReportSerializerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final AlertReportSerializer serializer = new AlertReportSerializer();

    @Test
    public void roundTrip() throws Exception {
        EquipmentAlert alert = new EquipmentAlert(42, "LINE-01-PRESS", "CRITICAL",
                "圧力異常", new Date(123456789L), "operator-1");
        File reportFile = temporaryFolder.newFile();

        serializer.saveReport(alert, reportFile.getAbsolutePath());
        EquipmentAlert restored;
        try (FileInputStream input = new FileInputStream(reportFile)) {
            restored = serializer.loadReport(input);
        }

        assertEquals(alert.getAlertId(), restored.getAlertId());
        assertEquals(alert.getEquipmentCode(), restored.getEquipmentCode());
        assertEquals(alert.getAlertLevel(), restored.getAlertLevel());
        assertEquals(alert.getMessage(), restored.getMessage());
        assertEquals(alert.getOccurredAt(), restored.getOccurredAt());
        assertEquals(alert.getReportedBy(), restored.getReportedBy());
    }

    @Test(expected = InvalidClassException.class)
    public void rejectsDisallowedClass() throws Exception {
        serializer.loadReport(new ByteArrayInputStream(serialize(new HashMap<Object, Object>())));
    }

    @Test(expected = InvalidClassException.class)
    public void rejectsCommonsCollectionsGadgetClass() throws Exception {
        Object gadget = LazyMap.decorate(new HashMap<Object, Object>(),
                (Transformer) new ConstantTransformer("x"));
        serializer.loadReport(new ByteArrayInputStream(serialize(gadget)));
    }

    @Test(expected = InvalidClassException.class)
    public void rejectsEquipmentAlertWrappedInDisallowedContainer() throws Exception {
        ArrayList<EquipmentAlert> alerts = new ArrayList<>();
        alerts.add(new EquipmentAlert(1, "LINE-02", "WARN", "温度上昇",
                new Date(987654321L), "operator-2"));
        serializer.loadReport(new ByteArrayInputStream(serialize(alerts)));
    }

    private byte[] serialize(Object value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        }
        return bytes.toByteArray();
    }
}
