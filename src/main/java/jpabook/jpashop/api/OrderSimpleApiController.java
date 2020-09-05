package jpabook.jpashop.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
}
