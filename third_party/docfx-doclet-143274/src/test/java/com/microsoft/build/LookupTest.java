package com.microsoft.build;

import static org.junit.Assert.*;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LookupTest {

  private Lookup lookup;
  private String packageUid = "package uid";
  private String packageNameWithType = "package name with type";
  private String classUid =
      "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String,boolean)";
  private String classNameWithType = "Person<T>.setFirstName(String firstName, boolean flag)";

  private List<MetadataFile> packageFiles =
      new ArrayList<>() {
        {
          MetadataFile packageFile = new MetadataFile("package path", "package name");
          packageFile.getItems().add(buildMetadataFileItem(packageUid, packageNameWithType));
          add(packageFile);
        }
      };
  private List<MetadataFile> classFiles =
      new ArrayList<>() {
        {
          MetadataFile classFile = new MetadataFile("class path", "class name");
          classFile.getItems().add(buildMetadataFileItem(classUid, classNameWithType));
          add(classFile);
        }
      };

  @Before
  public void setUp() {
    lookup = new Lookup(packageFiles, classFiles);
  }

  @Test
  public void buildContext() {
    LookupContext context = lookup.buildContext(classFiles.get(0));

    String[] localKeys = {
      // Uid as is
      "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String,boolean)",
      // Uid as is with spaces between params
      "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String, boolean)",
      // Uid with param types without package
      "com.microsoft.samples.subpackage.Person.setFirstName(String,boolean)",
      // Uid with param types without package with spaces between params
      "com.microsoft.samples.subpackage.Person.setFirstName(String, boolean)",
      // Uid without package
      "Person.setFirstName(java.lang.String,boolean)",
      // Uid without package with spaces between params
      "Person.setFirstName(java.lang.String, boolean)",

      // Name with type as is
      "Person<T>.setFirstName(String, boolean)",
      // Name with type without generics
      "Person.setFirstName(String firstName, boolean flag)",
      // Name with type without generics and param names
      "Person.setFirstName(String, boolean)",
      // Name with type without generics and param names without spaces between params
      "Person.setFirstName(String,boolean)"
    };
    assertEquals(
        "Wrong owner uid",
        context.getOwnerUid(),
        "Person.setFirstName(String firstName, boolean flag)");

    for (String localKey : localKeys) {
      assertTrue("Context should contain local key=" + localKey, context.containsKey(localKey));
      assertEquals("Wrong value for local key=" + localKey, context.resolve(localKey), classUid);
    }

    assertTrue("Context should contain global key", context.containsKey(packageNameWithType));
    assertTrue("Context should contain global value as a key", context.containsKey(packageUid));
    assertEquals("Wrong value for global key", context.resolve(packageNameWithType), packageUid);
    assertEquals("Wrong value for local value as a key", context.resolve(packageUid), packageUid);

    assertFalse("Context shouldn't contain unknown key", context.containsKey("unknown key"));
  }

  private MetadataFileItem buildMetadataFileItem(String uid, String nameWithType) {
    MetadataFileItem result = new MetadataFileItem(uid);
    result.setNameWithType(nameWithType);
    return result;
  }
}
