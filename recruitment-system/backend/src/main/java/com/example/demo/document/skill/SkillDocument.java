package com.example.demo.document.skill;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillDocument  {
//    @Id
//    @Field(type = FieldType.Long)
//    Long skillId;
    @JsonProperty("skill_id")
    @Field(name = "skill_id", type = FieldType.Long)
    Long skillId;

    @JsonProperty("skill_name")
    @Field(name = "skill_name", type = FieldType.Keyword)
    String skillName;
}
