package com.microsoft.build;

import static org.junit.Assert.assertEquals;

import com.microsoft.model.TocItem;
import com.microsoft.model.TocTypeMap;
import java.util.List;
import javax.lang.model.element.ElementKind;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YmlFilesBuilderTest {

  private YmlFilesBuilder ymlFilesBuilder;
  private DocletEnvironment environment;

  @Before
  public void setup() {
    environment = Mockito.mock(DocletEnvironment.class);
    ymlFilesBuilder =
        new YmlFilesBuilder(
            environment,
            "./target",
            new String[] {},
            new String[] {},
            "google-cloud-product",
            false,
            false,
            "0.18.0",
            "26.19.0",
            "./src/test/java/com/microsoft/samples/.repo-metadata.json");
  }

  @Test
  public void joinTocTypeItems() {
    TocTypeMap typeMap = new TocTypeMap();
    TocItem classToc = new TocItem("uid1", "name1");
    TocItem interfaceToc = new TocItem("uid2", "name2");
    TocItem enumToc = new TocItem("uid3", "name3");
    TocItem annotationToc = new TocItem("uid4", "name4");
    TocItem exceptionToc = new TocItem("uid5", "name5");

    typeMap.get(ElementKind.CLASS.name()).add(classToc);
    typeMap.get(ElementKind.INTERFACE.name()).add(interfaceToc);
    typeMap.get(ElementKind.ENUM.name()).add(enumToc);
    typeMap.get(ElementKind.ANNOTATION_TYPE.name()).add(annotationToc);
    typeMap.get("EXCEPTION").add(exceptionToc);

    List<TocItem> tocItems = ymlFilesBuilder.joinTocTypeItems(typeMap);

    assertEquals("Interfaces", tocItems.get(0).getHeading());
    assertEquals(interfaceToc, tocItems.get(1));

    assertEquals("Classes", tocItems.get(2).getHeading());
    assertEquals(classToc, tocItems.get(3));

    assertEquals("Enums", tocItems.get(4).getHeading());
    assertEquals(enumToc, tocItems.get(5));

    assertEquals("Annotation Types", tocItems.get(6).getHeading());
    assertEquals(annotationToc, tocItems.get(7));

    assertEquals("Exceptions", tocItems.get(8).getHeading());
    assertEquals(exceptionToc, tocItems.get(9));
  }
}
