package org.codavaj.process.docparser;

public interface Parser {

    boolean isSuitableVersion(String version);

    String getFirstIndexFileName();
}
