package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
	
	private final OrderRepository orderRepository;
	private final OrderSimpleQueryRepository orderSimpleQueryRepository;
	
	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1(){
		//이 api는 Order객체가 member로 Member객체를 참조하고 있고, 또 Member객체는 orders로 Order객체를 참조하고 있어서(양방향 연관관계) 직렬화시 순환참조로 인한 무한루프가 발생한다(Error). 
		//Order 객체를 참조하고 있는 모든 Entity에 @JsonIgnore를 붙인다. 그러나 프록시 객체에 의한 이슈가 또 발생한다. /jpashop/src/jpaproxy객체_직렬화_이슈.txt
		//첫번째 방법 : Hibernate5Module 옵션을 통해 해결한다.
		
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		
		//두번째 방법 : api 응답으로 필요한 것을 강제 초기화 한다.
		for(Order order : all) {
			order.getMember().getName(); //Lazy 강제 초기화
			order.getDelivery().getAddress(); //Lazy 강제 초기화
		}
		return all;
	}
	
	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2(){
		//ORDER 2개
		//N + 1 -> 1 + 회원 N + 배송 N
		List<SimpleOrderDto> orders = orderRepository.findAllByString(new OrderSearch())
												.stream()
												.map(SimpleOrderDto::new)
												.collect(Collectors.toList());
		return orders;
	}
	
	@GetMapping("/api/v3/simple-orders")
	public List<SimpleOrderDto> ordersV3(){
		//fetch join으로 N + 1 문제 해결
		List<Order> orders = orderRepository.findAllWithMemberDelivery();
		List<SimpleOrderDto> result = orders.stream()
				.map(o->new SimpleOrderDto(o))
				.collect(Collectors.toList());
		return result;
	}
	
	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDto> ordersV4(){
		//api에서 원하는 값만을 조회하여 Dto로 반환하지만 순수 Entity로 핸들링하는 Repository 컨셉에는 부합하지 않는 면도 있다.
		return orderSimpleQueryRepository.findOrderDtos();
	}
	
	@Data
	static class SimpleOrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		
		public SimpleOrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName(); //Lazy 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); //Lazy 초기화
		}
	}
}
