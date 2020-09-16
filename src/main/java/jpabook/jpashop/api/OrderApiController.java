package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
	
	private final OrderRepository orderRepository;
	private final OrderQueryRepository orderQueryRepository;
	
	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1(){
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for(Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();
			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName());
		}
		return all;
	}
	
	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2(){
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithItem();
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	//fetch join과 페이징시 이슈 해결법
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
			@RequestParam(value="offset", defaultValue="0") int offset,
			@RequestParam(value="limit", defaultValue="100") int limit) {
		//카디널리티 곱으로 결과가 출력되지 않는 1:1관계(xToOne)만 우선 조회
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
		List<OrderDto> result = orders.stream()
				//default_batch_fetch_size(글로벌 설정 전략) 또는 @BatchSize(부분적 설정 전략)를 설정하여 N에 해당하는 지연로딩 대상(OrderItem, Item)을 조회할 때
				//설정값의 한도만큼 in절에 PK를 꽉 채워 일괄조회 방식으로 데이터를 조회함.
				//첫번째 지연로딩 대상이 LAZY 초기화 될 때 일괄조회를 해줘서 1차캐시에도 그만큼 담기게 되어,
				//다음 대상 LAZY 초기화시 만일 1차캐시에 존재하면 쿼리대신 1차캐시에서 그냥 가져오므로 성능최적화 달성.
				/**
				 * from order_item orderitems0_ where orderitems0_.order_id in (4, 11)
				 */
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	@GetMapping("/api/v4/orders")
	public List<OrderQueryDto> ordersV4() {
		return orderQueryRepository.findOrderQueryDtos();
	}
	
	@GetMapping("/api/v5/orders")
	public List<OrderQueryDto> ordersV5() {
		return orderQueryRepository.findAllByDtoOptimization();
	}
	
	@Getter
	static class OrderDto {
		
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		//private List<OrderItem> orderItems; //OrderItem Entity를 api로 응답하는 문제 발생.
		private List<OrderItemDto> orderItems; //api로 응답할 Entity는 모두 Dto로 변화시켜 직렬화 되어지도록 함.
		
		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName();
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress();
			orderItems = order.getOrderItems().stream()
					.map(orderItem -> new OrderItemDto(orderItem))
					.collect(Collectors.toList());
		}
	}
	
	@Getter
	static class OrderItemDto {
		
		private String itemName; //상품명
		private int orderPrice; //주문가격
		private int count; //주문수량
		
		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
	}
}
