package in.btm.entity;

import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String street;
	private String city;
	private String state;
	private String zipCode;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserProfile user;
	
	@CreationTimestamp
	private LocalTime createDateTime;
	
	@UpdateTimestamp
	private  LocalTime updateDateTime;
} 