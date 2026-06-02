package ap1.jesus.huaripaucar.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "ai_query")
public class AIQuery {

    @Id
    @Field("_id")
    private String id;

    private String apiName;      
    private String prompt;      
    private String response;      
    private String estado;        
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String url;           
    private String lang;         
    private Integer engine;       

}