module com.sealforge {
    requires javafx.controls;
    requires java.prefs;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;

    exports com.sealforge.app;
    opens com.sealforge.app to javafx.graphics;
}
