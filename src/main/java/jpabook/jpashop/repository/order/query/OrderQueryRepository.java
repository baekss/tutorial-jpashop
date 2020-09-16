package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	public List<OrderQueryDto> findOrderQueryDtos() {
		List<OrderQueryDto> result = findOrders();
		
		result.forEach(o->{
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
			o.setOrderItems(orderItems);
		});
		return result;
	}
	
	//orderId 값으로 조회하여 OrderItemQueryDto를 완성시킴(N+1 쿼리).
	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
						"select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
						" from OrderItem oi"+
						" join oi.item i"+
						" where oi.order.id = :orderId", OrderItemQueryDto.class)
					.setParameter("orderId", orderId)
					.getResultList();
	}
	
	public List<OrderQueryDto> findAllByDtoOptimization() {
		List<OrderQueryDto> result = findOrders();
		
		List<Long> orderIds = toOrderIds(result);
		
		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);
		
		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
		return result;
	}

	//orderIds 값으로 in절 조회하여 OrderItemQueryDto를 1회 쿼리로 완성시킴.
	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		List<OrderItemQueryDto> orderItems = em.createQuery(
						"select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
						" from OrderItem oi"+
						" join oi.item i"+
						" where oi.order.id in :orderIds", OrderItemQueryDto.class)
					.setParameter("orderIds", orderIds)
					.getResultList();
		
		//key-value 구조로 만들어 O(1)로 활용하게 한다.
		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
													.collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
		return orderItemMap;
	}

	private List<Long> toOrderIds(List<OrderQueryDto> result) {
		List<Long> orderIds = result.stream()
							.map(o -> o.getOrderId())
							.collect(Collectors.toList());
		return orderIds;
	}
	
	//Dto로 바로 조회시 조인결과가 하나의 튜플로 딱 떨어지는 부분만 조회.
	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
						"select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
						" from Order o"+
						" join o.member m"+
						" join o.delivery d", OrderQueryDto.class)
					.getResultList();
	}
}
