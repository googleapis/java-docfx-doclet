package com.microsoft.samples.subpackage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class that describes some person
 *
 * <p>This comment has links to:
 *
 * <ul>
 *   <li>Owner class {@link Person}
 *   <li>Its inner class {@link Person.IdentificationInfo}
 *   <li>Its method {@link Person#setLastName(String lastName)}
 *   <li>Its method without params {@link Person#setLastName()}
 *   <li>Its public field {@link Person#age}
 *   <li>Another class which used here {@link Set}
 *   <li>Another class which not used here {@link List}
 *   <li>Broken link {@link sdfdsagdsfghfgh}
 *   <li>Plain link {@linkplain someContent}
 *   <li>Link that starts from '#' {@link #setLastName()}
 *   <li>Link with label {@link Set WordOne}
 * </ul>
 *
 * This is an "at" symbol: {@literal @}
 *
 * @see Display
 */
public class Person<T> {

  private String firstName;
  private String lastName;
  public int age;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setLastName() {
    this.lastName = null;
  }

  public Set<String> getSomeSet() {
    return Collections.emptySet();
  }

  /**
   * We need to have this method that takes parameter and return types declared in the current class
   */
  public static Person buildPerson(Person seed) {
    return seed;
  }

  /** Class that describes person's identification */
  public static class IdentificationInfo {

    /** Enum describes person's gender */
    public enum Gender {
      MALE,
      FEMALE
    }
  }
}
