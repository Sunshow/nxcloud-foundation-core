package nxcloud.foundation.core.assembler.component;

import nxcloud.foundation.core.assembler.annotation.AssemblyRefSource;

public class TestJavaInheritableEntity {
    private Long id;

    private String name;

    @AssemblyRefSource(source = TestJavaRefEntity.class, sourceField = "id")
    private Long refId;

    public TestJavaInheritableEntity() {
    }

    public TestJavaInheritableEntity(Long id, String name, Long refId) {
        this.id = id;
        this.name = name;
        this.refId = refId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }
}
