package ap1.jesus.huaripaucar.model.dto;

import lombok.Data;

@Data
public class SummarizerRequest {
    private String url;              
    private String lang = "es";      
    private Integer engine = 2;     
}