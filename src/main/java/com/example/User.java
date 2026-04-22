package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")

public class User {
	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(unique = true, nullable = false)
	    private String username;

	    @Column(nullable = false)
	    private String password;

		@Column(nullable = false)
		private String diet;
		
		@Column(nullable = true)
		private String allergies;


	    public User() {
	    }

	    public User(String username, String password, String diet,String allergies) {
	        this.username = username;
	        this.password = password;
	        this.diet = diet;
	        this.allergies = allergies;
	    }

	    public Long getId() {
	        return id;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public String getDiet() {
	        return diet;
	    }

	    public void setDiet(String diet) {
	        this.diet = diet;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	        
	    }
	    
	    public String getAllergies() {
	        return allergies;
	    }

	    public void setAllergies(String allergies) {
	        this.allergies = allergies;
	    }

}
