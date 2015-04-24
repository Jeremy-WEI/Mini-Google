package cis555.indexer;

import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import cis555.utils.UrlDocIDMapper;

public class PDFIndexer extends Indexer {

    public PDFIndexer(InputStream is, String URL, long docID, UrlDocIDMapper db)
            throws Exception {
        super(is, URL, docID, db);
        PDDocument document = PDDocument.load(this.is);
        // PDDocumentInformation info = document.getDocumentInformation();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(Integer.MAX_VALUE);
        content = stripper.getText(document);
    }

}
