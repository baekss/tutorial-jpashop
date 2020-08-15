package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
	
	//@PersistenceContext로 field injection을 하는게 표준이지만 spring boot에서는 @Autowired로도 field injection이 가능하다.
	//field injection이 아닌 @RequiredArgsConstructor로 생성자를 이용한 injection을 사용하였다.
	private final EntityManager em;
	
	public void save(Member member) {
		em.persist(member);
	}
	
	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}
	
	public List<Member> findAll() {
		return em.createQuery("SELECT m FROM Member m", Member.class)
				.getResultList();
	}
	
	public List<Member> findByName(String name) {
		return em.createQuery("SELECT m FROM Member m WHERE m.name = :name", Member.class)
				.setParameter("name", name)
				.getResultList();
	}
}
