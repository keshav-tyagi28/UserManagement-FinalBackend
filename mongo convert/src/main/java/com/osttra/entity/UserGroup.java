
package com.osttra.entity;



import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UserGroup")
public class UserGroup {

	@Id
	private String groupId; 
	private String groupName;
	private String description;
	
	
	private Set<String> userId = new HashSet<>();
	

	
	
	
}