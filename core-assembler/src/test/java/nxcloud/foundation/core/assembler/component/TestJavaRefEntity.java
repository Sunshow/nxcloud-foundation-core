package nxcloud.foundation.core.assembler.component;

public class TestJavaRefEntity {
    private Long id;
    private String description;

    public TestJavaRefEntity() {
    }

    public TestJavaRefEntity(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
