package nxcloud.foundation.core.assembler.component;

import nxcloud.foundation.core.assembler.annotation.AssemblableWrapper;
import nxcloud.foundation.core.assembler.annotation.AssemblyField;

@AssemblableWrapper
public class TestJavaInheritanceWrapperEntity extends TestJavaInheritableEntity {

    @AssemblyField
    private TestJavaRefEntity ref;

    @AssemblyField(entityField = "refId", targetField = "description")
    private String description;

    public TestJavaInheritanceWrapperEntity() {
        super();
    }

    public TestJavaInheritanceWrapperEntity(Long id, String name, Long refId) {
        super(id, name, refId);
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
