package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class DependencyExportDTO {
    private Long id;
    private String fromExportId;
    private String toExportId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromExportId() { return fromExportId; }
    public void setFromExportId(String fromExportId) { this.fromExportId = fromExportId; }

    public String getToExportId() { return toExportId; }
    public void setToExportId(String toExportId) { this.toExportId = toExportId; }
}
