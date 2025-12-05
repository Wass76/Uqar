package com.Uqar.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for pharmaceutical import functionality
 */
@Configuration
@ConfigurationProperties(prefix = "pharmaceutical")
public class PharmaceuticalImportConfig {

    private Python python = new Python();
    private Temp temp = new Temp();
    private Import importConfig = new Import();

    public Python getPython() {
        return python;
    }

    public void setPython(Python python) {
        this.python = python;
    }

    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

    public Import getImport() {
        return importConfig;
    }

    public void setImport(Import importConfig) {
        this.importConfig = importConfig;
    }

    public static class Python {
        private Script script = new Script();

        public Script getScript() {
            return script;
        }

        public void setScript(Script script) {
            this.script = script;
        }

        public static class Script {
            private String path = "scripts/";

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }
        }
    }

    public static class Temp {
        private String dir = "/tmp/pharmaceutical/";

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    public static class Import {
        private Timeout timeout = new Timeout();
        private MaxFileSize maxFileSize = new MaxFileSize();

        public Timeout getTimeout() {
            return timeout;
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }

        public MaxFileSize getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(MaxFileSize maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public static class Timeout {
            private int minutes = 5;

            public int getMinutes() {
                return minutes;
            }

            public void setMinutes(int minutes) {
                this.minutes = minutes;
            }
        }

        public static class MaxFileSize {
            private int mb = 50;

            public int getMb() {
                return mb;
            }

            public void setMb(int mb) {
                this.mb = mb;
            }
        }
    }
}
