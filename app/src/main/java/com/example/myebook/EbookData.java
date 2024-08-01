package com.example.myebook;

public class EbookData {
    private String pdfname, pdfUrl;

    public EbookData() {
    }

    public EbookData(String name, String pdfUrl) {
        this.pdfname = name;
        this.pdfUrl = pdfUrl;
    }

    public String getPdfname() {
        return pdfname;
    }

    public void setPdfname(String name) {
        this.pdfname = name;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
