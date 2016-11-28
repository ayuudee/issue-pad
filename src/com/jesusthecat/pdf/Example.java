package com.jesusthecat.pdf;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;
import java.nio.file.Paths;

class Example {

  /**
   * This does what we'd expect.
   * Example d1.pdf out.pdf
   *
   * This DOES NOT do what we'd expect.
   * Example d2.pdf out.pdf
   */
  public static void main(String[] argv) throws Exception {
    if (argv.length != 2) {
      throw new IllegalArgumentException("Expected two arguments");
    }

    File in = Paths.get(argv[0]).toAbsolutePath().normalize().toFile();
    File out = Paths.get(argv[1]).toAbsolutePath().normalize().toFile();

    if (!(in.exists() && out.canWrite() && in != out)) {
      throw new IllegalArgumentException(
        "Wanted appropriate in/out paths as arg 1 and arg2.");
    }

    PDDocument doc = PDDocument.load(in);
    PDDocumentCatalog cat  = doc.getDocumentCatalog();
    PDAcroForm form = cat.getAcroForm();
    PDField f = form.getField("foo");
    PDPage page = f.getWidgets().get(0).getPage();

    PDRectangle rect = f.getWidgets().get(0).getRectangle();

    // This would attempt to write a 5x5 black square at the bottom
    // LHS of the bounding box of the field 'foo.' In D2 it appears
    // somewhere offset left and above. In D1 it appears more-or-less
    // where we would expect.
    PDPageContentStream cStream =
      new PDPageContentStream(doc, page, AppendMode.APPEND, true);
    cStream.addRect(
      rect.getLowerLeftX(),
      rect.getLowerLeftY(),
      5, // Width
      5); // Height
    cStream.fill();

    doc.save(out);
    doc.close();
  }

}
