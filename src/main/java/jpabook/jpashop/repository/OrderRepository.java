package jpabook.jpashop.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	private final EntityManager em;
	
	public void save(Order order) {
		em.persist(order);
	}
	
	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}
	
	public List<Order> findAllByString(OrderSearch orderSearch){
		String jpql = "select o from Order o join o.member m"; 
		boolean isFirstCondition = true;
		
		//주문 상태 검색
		if(orderSearch.getOrderStatus() != null) {
			if(isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			}else {
				jpql += " and";
			}
			jpql += " o.status = :status";
		}
		
		//회원 이름 검색
		if(StringUtils.hasText(orderSearch.getMemberName())) {
			if(isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			}else {
				jpql += " and";
			}
			jpql += " m.name like CONCAT('%', :name, '%')";
		}
		
		TypedQuery<Order> query = em.createQuery(jpql, Order.class)
									.setMaxResults(1000);
		
		if(orderSearch.getOrderStatus() != null) {
			query.setParameter("status", orderSearch.getOrderStatus());
		}
		
		if(StringUtils.hasText(orderSearch.getMemberName())) {
			query.setParameter("name", orderSearch.getMemberName());
		}
		
		return query.getResultList();
	}
	
	public List<Order> findAllByCriteria(OrderSearch orderSearch) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> o = cq.from(Order.class);
		Join<Object, Object> m = o.join("member", JoinType.INNER);
		
		List<Predicate> criteria = new ArrayList<>();
		
		//주문 상태 검색
		if(orderSearch.getOrderStatus() != null) {
			Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
			criteria.add(status);
		}
		
		//회원 이름 검색
		if(StringUtils.hasText(orderSearch.getMemberName())) {
			Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
			criteria.add(name);
		}
		
		cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
		return query.getResultList(); 
	}

	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery(
				"select o from Order o"+
				" join fetch o.member m"+
				" join fetch o.delivery d", Order.class
				).getResultList();
	}
	
	public List<Order> findAllWithItem() {
		//fetch join 시 1:N 관계에 의해 Order 1개에 OrderItem이 N개 이므로 결과도 카디널리티 곱으로 나온다.
		//따라서 동일한 Order를 N개로 받지 않기 위해 JPQL의 distinct를 적용한다.
		//JPQL의 distinct는 SQL상으로도 distinct를 붙여주며, 반환된 Entity가 중복일 땐 중복을 걸러서 반환해 주는 역할을 한다. 
		return em.createQuery(
				"select distinct o from Order o"+
				" join fetch o.member m"+
				" join fetch o.delivery d"+
				" join fetch o.orderItems oi"+
				" join fetch oi.item i", Order.class)
				//.setFirstResult(1)
				//.setMaxResults(10)
				.getResultList();
		//collection fetch join 단점은 페이징 처리시 SQL상으로 페이징을 적용(ex. limit, offset 키워드)하지 않는다는 점이다.
		//경고를 띄우며 어플리케이션 메모리 상에서 페이징 처리를 한다(firstResult/maxResults specified with collection fetch; applying in memory!).
		//따라서 메모리 관련 Error가 발생할 수도 있다.
	}
}
