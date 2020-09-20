package jpabook.jpashop.service.query;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class OrderQueryService {
	
	private final OrderRepository orderRepository;
	
	//Open Session in View(OSIV) 가 false일때는 영속성 컨텍스트의 라이프 사이클이 Transaction 범위까지로 한정됨. 즉, 지연로딩을 Service 에서 완료시켜야 한다.
	public List<OrderDto> ordersV2(){
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		
		return result;
	}
}
