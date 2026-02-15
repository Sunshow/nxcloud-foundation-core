package nxcloud.foundation.core.assembler.component;

import nxcloud.foundation.core.assembler.annotation.AssemblableWrapper;
import nxcloud.foundation.core.assembler.annotation.AssemblyEntity;
import nxcloud.foundation.core.assembler.annotation.AssemblyField;

@AssemblableWrapper
public class TestJavaWrapperEntity {
    @AssemblyEntity
    private TestJavaSourceEntity source;

    @AssemblyField
    private TestJavaRefEntity ref;

    @AssemblyField(entityField = "refId", targetField = "description")
    private String description;

    public TestJavaSourceEntity getSource() {
        return source;
    }

    public void setSource(TestJavaSourceEntity source) {
        this.source = source;
    }

    public TestJavaRefEntity getRef() {
        return ref;
    }

    public void setRef(TestJavaRefEntity ref) {
        this.ref = ref;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
