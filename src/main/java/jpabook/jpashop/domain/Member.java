package jpabook.jpashop.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="member_id")
	private Long id;
	
	@NotEmpty
	private String name;
	
	@Embedded
	private Address address;
	
	//@JsonIgnore api 스펙에 종속적인 컨셉을 Entity에서 사용하지 말자.
	@OneToMany(mappedBy="member")
	private List<Order> orders = new ArrayList<>();
}
