package eu.ladybug.muse;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

    //private static final String patchFile = "/Users/neugens/Downloads/MUSE-LIB-001 FACTORY/library/bank02/patch16/visionary eyes.mmp";
    private static final String patchDirectory= "/Users/neugens/Downloads/MUSE-LIB-001 FACTORY/library";
    private static final String patchesOut= "/Users/neugens/Downloads/MUSE-LIB-001 FACTORY/normalised.txt";

    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(patchDirectory);
        List<String> patches = new ArrayList<>();
        Files.walk(dir).forEach(path -> {
            try {
                if (path.toFile().getName().endsWith(".mmp")) {
                    patches.add(analyse(path.toFile()));
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        PrintWriter out = new PrintWriter(patchesOut);
        patches.forEach(s -> out.println(s));
        out.close();
    }

    public static String analyse(File file) throws Exception {
        Path filePath = Path.of(file.getPath());
        return scanPatch(filePath);
    }

    public static String scanPatch(Path patchFile) throws Exception {

        Charset charset = StandardCharsets.UTF_8;
        SortedMap<String, Value> patch = new TreeMap<>();
        String currentKey = null;

        List<String> lines = Files.readAllLines(patchFile, charset);
        String version = "";
        for (String line : lines) {
            switch (line) {
                case String _version when _version.startsWith("Version") -> {
                    version = _version;
                }

                case String key when key.contains(":") -> {
                    currentKey = key;
                }

                case String value when value.contains("timbreValues") -> {

                    String timbreValues = value.replaceAll("timbreValues = ", "").strip();
                    String key = currentKey.replaceAll(":", "").strip();

                    if (timbreValues.contains("\"") || timbreValues.contains("true") || timbreValues.contains("false")) {
                        // ControlValue
                        patch.put(key, ControlValue.createValue(timbreValues));

                    } else {
                        // Knob value
                        patch.put(key, KnobValue.createValue(timbreValues));
                    }
                }

                case String value when value.contains("value") -> {
                    String timbreValues = value.replaceAll("value = ", "").strip();
                    String key = currentKey.replaceAll(":", "").strip();
                    patch.put(key, GlobalValue.createValue(timbreValues));
                }

                default -> {}
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Patch name: ").append(patchFile.getFileName()).append(System.lineSeparator());
        builder.append(version);
        builder.append(System.lineSeparator());
        for (String key : patch.keySet()) {
            builder.append(key + " -> " + patch.get(key) + "" + System.lineSeparator());
        }

        return builder.toString();
    }

    private static long getValueNormalized(double value) {
        return Math.round(value * 127.);
    }

    private static long getPercent(double value) {
        return Math.round(value * 100);
    }

    public static boolean isFloatingPoint(String value) {
        return value.matches("-?\\d+(\\.\\d+)?") && value.contains(".");
    }

    private static abstract class Value {
        abstract String print();

        public String toString() {
            return print();
        }
    }

    private static class ControlValue extends Value {

        private String timbreA;
        private String timbreB;

        public static Value createValue(String definition) {
            ControlValue value = new ControlValue();
            String[] rangeValues = definition.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(";", "").strip().split(",");
            value.timbreA = rangeValues[0].strip();
            value.timbreB = rangeValues[1].strip();

            return value;
        }

        @Override
        public String print() {
            return "Timbre A: " + timbreA + ", Timbre B: " + timbreB;
        }
    }

    private static class KnobValue extends Value {

        private double rv0;
        private double rv1;

        private long nomarlisedRV0;
        private long nomarlisedRV1;

        private long percentRV0;
        private long percentRV1;

        static Value createValue(String definition) {

            KnobValue value = new KnobValue();
            try {
                String[] rangeValues = definition.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(";", "").strip().split(",");

                value.rv0 = Double.valueOf(rangeValues[0]);
                value.rv1 = Double.valueOf(rangeValues[1]);

                value.nomarlisedRV0 = getValueNormalized(value.rv0);
                value.nomarlisedRV1 = getValueNormalized(value.rv1);

                value.percentRV0 = getPercent(value.rv0);
                value.percentRV1 = getPercent(value.rv1);

            } catch (NumberFormatException notANumber) {}

            return value;
        }

        @Override
        public String print() {
            return "Timbre A: " + rv0 + ", Timbre B: " + rv1 + " normalised: A: " + nomarlisedRV0 + ", B " + nomarlisedRV1 + "; Percent A: " + percentRV0 + "% , B: " + percentRV1 + "%";
        }
    }

    private static class GlobalValue extends Value {

        String value;
        String normalised;

        public static Value createValue(String definition) {
            GlobalValue value = new GlobalValue();
            value.value = definition.replaceAll("\"", "").replaceAll(";", "").strip();
            value.normalised = value.value;
            if (isFloatingPoint(value.value)) {
                value.normalised = String.valueOf(getValueNormalized(Double.parseDouble(value.value)));
            }
            return value;
        }

        @Override
        String print() {
            return "Global Value: " + value + ", normalised: " + normalised;
        }
    }
}
