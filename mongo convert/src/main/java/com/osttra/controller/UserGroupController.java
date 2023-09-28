package com.osttra.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.osttra.entity.User;
import com.osttra.entity.UserGroup;


import com.osttra.service.UserDetailServiceImpl;
import com.osttra.service.UserGroupDetailsServiceImpl;

import com.osttra.to.CustomResponse;


@RestController
@CrossOrigin
@RequestMapping("/usergroups")
public class UserGroupController {
	
	
	

	@Autowired
	UserGroupDetailsServiceImpl usergroupdetailservice;
	
	@Autowired
	UserDetailServiceImpl userdetailservice;
	
	 @Autowired
	private RestTemplate restTemplate;

	private String ip="10.196.22.55:8080";
	////////////////////////////////////////////////// CRUD ///////////////////////////////////////////////////////////////////////////////

	
	@PostMapping("/registerusergroup")
	public ResponseEntity<?> addUserGroup(@RequestBody UserGroup userGroup, HttpServletRequest request) {

		UserGroup existingUserGroup = usergroupdetailservice.getUserGroupById(userGroup.getGroupId());

		if (existingUserGroup != null) {

			CustomResponse<String> errorResponse = new CustomResponse<>("", "Duplicate Group", 409,
					request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
		}

		UserGroup savedUserGroup = usergroupdetailservice.saveUserGroup(userGroup);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("id", userGroup.getGroupId());
		jsonObject.put("name", userGroup.getGroupName());
		jsonObject.put("type", "organizational Unit");

		String externalApiUrl = "http://" + ip + "/engine-rest/group/create";
		String jsonPayload = jsonObject.toString();
//		ResponseEntity<String> responseEntity = userdetailservice.sendJsonToExternalApi(externalApiUrl, HttpMethod.POST,
//				jsonPayload);

		CustomResponse<UserGroup> successResponse = new CustomResponse<>(savedUserGroup,
				"User group added successfully", 200, request.getServletPath());
		return new ResponseEntity<>(successResponse, HttpStatus.OK);

	}
		
		
	@GetMapping(value = "/allgroups", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> getAllUserGroups(HttpServletRequest request) {
		try {

			List<UserGroup> usergroups = usergroupdetailservice.getAllUserGroups();

			CustomResponse<List<UserGroup>> successResponse = new CustomResponse<>(usergroups, "Listed all user groups",
					HttpStatus.OK.value(), request.getServletPath());
			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {

			CustomResponse<List<UserGroup>> errorResponse = new CustomResponse<>(null, "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

		} catch (Exception e) {

			CustomResponse<List<UserGroup>> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
		
		
	@PutMapping("/updategroup/{usergroupid}")
	public ResponseEntity<?> updateUserGroup(@PathVariable String usergroupid, @RequestBody UserGroup updatedUserGroup,
			HttpServletRequest request) {

		try {

			UserGroup existingUserGroup = usergroupdetailservice.getUserGroupById(usergroupid);

			if (existingUserGroup == null) {

				CustomResponse<String> errorResponse = new CustomResponse<>("", "User group not found",
						HttpStatus.NOT_FOUND.value(), request.getServletPath());
				return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

			}

			existingUserGroup.setGroupName(updatedUserGroup.getGroupName());
			existingUserGroup.setDescription(updatedUserGroup.getDescription());
			usergroupdetailservice.saveUserGroup(existingUserGroup);

			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode jsonObject = objectMapper.createObjectNode();

			jsonObject.put("id", usergroupid);
			jsonObject.put("name", existingUserGroup.getGroupName());
			jsonObject.put("type", "userGroup");

			String externalApiUrl = "http://" + ip + "//engine-rest/group/" + usergroupid;

			String jsonPayload = jsonObject.toString();
//			ResponseEntity<String> responseEntity = userdetailservice.sendJsonToExternalApi(externalApiUrl,
//					HttpMethod.PUT, jsonPayload);

			CustomResponse<UserGroup> successResponse = new CustomResponse<>(
					usergroupdetailservice.saveUserGroup(existingUserGroup), "User group updated successfully",
					HttpStatus.OK.value(), request.getServletPath());
			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {

			CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

		} catch (Exception e) {

			CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
		
	     
	@DeleteMapping("/delete/{usergroupid}")
	public ResponseEntity<?> deleteUser(@PathVariable String usergroupid, HttpServletRequest request) {
		try {
			UserGroup userGroupToDelete = usergroupdetailservice.getUserGroupById(usergroupid);

			if (userGroupToDelete == null) {
				CustomResponse<User> errorResponse = new CustomResponse<>(null, "User group not found",
						HttpStatus.NOT_FOUND.value(), request.getRequestURI());

				return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
			}

			for (String temp : userGroupToDelete.getUserId()) {

				ResponseEntity<Object> result = removeUserGroup(temp, usergroupid, request);

			}

			String externalApiUrl = "http://" + ip + "/engine-rest/group/" + usergroupid;

//			ResponseEntity<String> responseEntity = userdetailservice.sendJsonToExternalApi(externalApiUrl,
//					HttpMethod.DELETE, null);

			usergroupdetailservice.deleteUserGroup(usergroupid);

			CustomResponse<UserGroup> successResponse = new CustomResponse<>(userGroupToDelete,
					"User group deleted successfully", HttpStatus.OK.value(), request.getRequestURI());
			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getRequestURI());

			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	    
	    //////////////////////////////////////////// Details of specific user group ////////////////////////////////////////////////////////////
	    
	    @GetMapping("findusergroup/{usergroupid}")
		@ResponseBody
		public ResponseEntity<?> getSpecificUserGroup(@PathVariable String usergroupid, HttpServletRequest request) {
		    try {
		    	
		        UserGroup userGroup = usergroupdetailservice.getUserGroupById(usergroupid);

		        if (userGroup == null) {
		        	
		            CustomResponse<String> errorResponse = new CustomResponse<>("", "User group not found", HttpStatus.NOT_FOUND.value(), request.getServletPath());
		            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		            
		        }

		        CustomResponse<UserGroup> successResponse = new CustomResponse<>(userGroup, "User group found", HttpStatus.OK.value(), request.getServletPath());
		        return new ResponseEntity<>(successResponse, HttpStatus.OK);
		        
		    } catch (IllegalArgumentException e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		        
		    } catch (Exception e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		        
		    }
		}
	    
	    
	    ////////////////////////////////////////// Users present in specific group /////////////////////////////////////////////////
	    
	    @GetMapping("/{usergroupid}/users")
		@ResponseBody
		public ResponseEntity<?> getUserGroups(@PathVariable String usergroupid, HttpServletRequest request) {
			
			try {
				
				UserGroup userGroup = usergroupdetailservice.getUserGroupById(usergroupid);

				if (userGroup == null) {
					
					CustomResponse<String> errorResponse = new CustomResponse<>("", "User group not found", HttpStatus.NOT_FOUND.value(), request.getServletPath());
		            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		            
				}

				Set<String> userList = userGroup.getUserId();
				
				if(userList.size()==0)
				{
					CustomResponse<String> errorResponse = new CustomResponse<>("", "No users present in this group", HttpStatus.NO_CONTENT.value(), request.getServletPath());
		            return new ResponseEntity<>(errorResponse, HttpStatus.NO_CONTENT);
				}
				Set<User> users = new HashSet<User> (); 
				
				for (String temp : userList) {
					
					User user = userdetailservice.getUserById(temp);
					users.add(user);
					
				}
				
				CustomResponse<Set<User>> successResponse = new CustomResponse<>(users, " Group Users  displayed succesfully", HttpStatus.OK.value(), request.getServletPath());
		        return new ResponseEntity<>(successResponse, HttpStatus.OK);
				
			} catch (IllegalArgumentException e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		        
		    } catch (Exception e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		        
		    }

		}
	    
	    
	    ///////////////////////////////////////////////// Mapping //////////////////////////////////////////////////////////////////
	    
	    
	    @PostMapping("/addusers/{groupId}")
	    public ResponseEntity<Object> addmultiple(@RequestBody Map<String, String[]> requestBody, @PathVariable String groupId, HttpServletRequest request) {
	       
	    	 String[] userIds = requestBody.get("username");
	    	 
	        for (String userId : userIds) {
	        	User user = userdetailservice.getUserById(userId);

				if (user != null) {
	        	ResponseEntity<Object> response= addUserGroup(userId, groupId,request);
				}
				else
				{
					CustomResponse<String> errorResponse = new CustomResponse<>("", "User not found",
							HttpStatus.NOT_FOUND.value(), request.getServletPath());
					return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
				}
				
				
				
	        }
	        
	        return ResponseEntity.ok("Users added successfully"); 
	    
	    }
	    
	    
	    
		@PostMapping("/addusergroup/{userId}/{groupId}")
		public ResponseEntity<Object> addUserGroup(@PathVariable String userId, @PathVariable String groupId,
				HttpServletRequest request) {

			User user = userdetailservice.getUserById(userId);

			UserGroup userGroup = usergroupdetailservice.getUserGroupById(groupId);

			if (user != null && userGroup != null) {

				user.getUserGroupsId().add(userGroup.getGroupId());
				userdetailservice.saveUser(user);

				userGroup.getUserId().add(user.getUsername());
				usergroupdetailservice.saveUserGroup(userGroup);

				String externalApiUrl = "http://" + ip + "/engine-rest/group/" + groupId + "/members/" + userId;

//				ResponseEntity<String> responseEntity = userdetailservice.sendJsonToExternalApi(externalApiUrl,
//						HttpMethod.PUT, "");

				CustomResponse<String> errorResponse = new CustomResponse<>("", "User added successfully to UserGroup",
						HttpStatus.OK.value(), request.getServletPath());
				return new ResponseEntity<>(errorResponse, HttpStatus.OK);
			} else {

				CustomResponse<String> errorResponse = new CustomResponse<>("", "User or UserGroup not found",
						HttpStatus.NOT_FOUND.value(), request.getServletPath());
				return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

			}

		}
	    
		@PostMapping("/removeusergroup/{userId}/{groupId}")
		public ResponseEntity<Object> removeUserGroup(@PathVariable String userId, @PathVariable String groupId,
				HttpServletRequest request) {

			User user = userdetailservice.getUserById(userId);

			UserGroup userGroup = usergroupdetailservice.getUserGroupById(groupId);

			if (user != null && userGroup != null) {

				user.getUserGroupsId().remove(userGroup.getGroupId());
				userdetailservice.saveUser(user);

				userGroup.getUserId().remove(user.getUsername());
				usergroupdetailservice.saveUserGroup(userGroup);

				String externalApiUrl = "http://" + ip + "/engine-rest/group/" + groupId + "/members/" + userId;

//				ResponseEntity<String> responseEntity = userdetailservice.sendJsonToExternalApi(externalApiUrl,
//						HttpMethod.DELETE, "");

				CustomResponse<String> errorResponse = new CustomResponse<>("",
						"User removed successfully from UserGroup", HttpStatus.OK.value(), request.getServletPath());
				return new ResponseEntity<>(errorResponse, HttpStatus.OK);

			} else {
				CustomResponse<String> errorResponse = new CustomResponse<>("", "User or UserGroup not found",
						HttpStatus.NOT_FOUND.value(), request.getServletPath());
				return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
			}
		}
	
	    
	    
	    
	    
	    
@GetMapping("/{usergroupid}/usersnotingroup")
@ResponseBody
public ResponseEntity<?> getUserNotInGroups(@PathVariable String usergroupid, HttpServletRequest request) {


	try {

		

		UserGroup userGroup = usergroupdetailservice.getUserGroupById(usergroupid);



		if (userGroup == null) {

			

			CustomResponse<String> errorResponse = new CustomResponse<>("", "User group not found", HttpStatus.NOT_FOUND.value(), request.getServletPath());

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

            

		}



		Set<String> userList = userGroup.getUserId();

		

		List<User> allUsers = userdetailservice.getAllUser();

		

		Set<User> usersNotInGroup = new HashSet<User> ();

		

		for(User temp : allUsers) {

			if( !userList.contains(temp.getUsername()) && !temp.getRole().equals("ROLE_ADMIN") ) {

				User user = userdetailservice.getUserById(temp.getUsername());

				usersNotInGroup.add(user);

			}

		}

		

		CustomResponse<Set<User>> successResponse = new CustomResponse<>(usersNotInGroup, "User displayed succesfully", HttpStatus.OK.value(), request.getServletPath());

        return new ResponseEntity<>(successResponse, HttpStatus.OK);

		

	} catch (IllegalArgumentException e) {



        CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getServletPath());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        

    } catch (Exception e) {



        CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        

    }



}
}
	    
	    
	
	

