package com.microsoft.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class MetadataFileItemTest {

    @Test
    public void testEquals() {
        MetadataFileItem object1 = new MetadataFileItem("123");
        MetadataFileItem object2 = new MetadataFileItem("1234");
        MetadataFileItem object3 = new MetadataFileItem("123");

        assertTrue("Should be equal to self", object1.equals(object1));
        assertFalse("Should not be equal to null", object1.equals(null));
        assertFalse("Should not be equal to object of another type", object1.equals(123));
        assertFalse("Should not be equal to object with another uid", object1.equals(object2));
        assertTrue("Should be equal to object with same uid", object1.equals(object3));
    }

    @Test
    public void testHashCode() {
        String uid = "123";
        MetadataFileItem object = new MetadataFileItem(uid);

        assertEquals("Wrong result", uid.hashCode(), object.hashCode());
    }

    @Test
    public void setTypeParameters() {
        MetadataFileItem object = new MetadataFileItem("123");
        List<TypeParameter> typeParams = new ArrayList<>();

        object.setTypeParameters(typeParams);

        assertNotNull("Syntax should not be null", object.getSyntax());
        assertEquals("Wrong typeParameters value", object.getSyntax().getTypeParameters(), typeParams);
    }

    @Test
    public void setTypeParametersWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        List<TypeParameter> typeParams = new ArrayList<>();

        object.setTypeParameters(typeParams);

        assertEquals("Syntax object should remain the same", object.getSyntax(), existingSyntax);
        assertEquals("Wrong typeParameters value", object.getSyntax().getTypeParameters(), typeParams);
    }

    @Test
    public void setParameters() {
        MetadataFileItem object = new MetadataFileItem("123");
        List<MethodParameter> params = new ArrayList<>();

        object.setParameters(params);

        assertNotNull("Syntax should not be null", object.getSyntax());
        assertEquals("Wrong parameters value", object.getSyntax().getParameters(), params);
    }

    @Test
    public void setParametersWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        List<MethodParameter> params = new ArrayList<>();

        object.setParameters(params);

        assertEquals("Syntax object should remain the same", object.getSyntax(), existingSyntax);
        assertEquals("Wrong parameters value", object.getSyntax().getParameters(), params);
    }

    @Test
    public void setReturn() {
        MetadataFileItem object = new MetadataFileItem("123");
        Return returnValue = new Return("type");

        object.setReturn(returnValue);

        assertNotNull("Syntax should not be null", object.getSyntax());
        assertEquals("Wrong return value", object.getSyntax().getReturnValue(), returnValue);
    }

    @Test
    public void setReturnWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        Return returnValue = new Return("type");

        object.setReturn(returnValue);

        assertEquals("Syntax object should remain the same", object.getSyntax(), existingSyntax);
        assertEquals("Wrong return value", object.getSyntax().getReturnValue(), returnValue);
    }

    @Test
    public void setContent() {
        MetadataFileItem object = new MetadataFileItem("123");
        String content = "Some content";

        object.setContent(content);

        assertNotNull("Syntax should not be null", object.getSyntax());
        assertEquals("Wrong content value", object.getSyntax().getContent(), content);
    }

    @Test
    public void setContentWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        String content = "Some content";

        object.setContent(content);

        assertEquals("Syntax object should remain the same", object.getSyntax(), existingSyntax);
        assertEquals("Wrong content value", object.getSyntax().getContent(), content);
    }

    @Test
    public void setInheritance() {
        MetadataFileItem object = new MetadataFileItem("123");

        object.setInheritance(Arrays.asList("Some value"));

        assertEquals("Wrong inheritance size", object.getInheritance().size(),1);
        assertThat("Wrong inheritance content", object.getInheritance(), hasItem("Some value"));
    }

    @Test
    public void setInheritanceForNull() {
        MetadataFileItem object = new MetadataFileItem("123");

        object.setInheritance(null);

        assertNull("Wrong inheritance", object.getInheritance());
    }

    @Test
    public void getIsExternal() {
        assertNull("Wrong isExternal when null", (new MetadataFileItem("123")).getIsExternal());
        assertTrue("Wrong isExternal when true", (new MetadataFileItem("123", "name", true)).getIsExternal());
        assertNull("Wrong isExternal when false", (new MetadataFileItem("123", "name", false)).getIsExternal());
    }
}
