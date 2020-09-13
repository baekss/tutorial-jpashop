package jpabook.jpashop;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;

/**
 * 총 주문 2개
 * userA
 * 	JPA1 BOOK
 * 	JPA2 BOOK
 * userB
 * 	SPRING1 BOOK
 * 	SPRING2 BOOK
 */
@Component
@RequiredArgsConstructor
public class InitDb {

	private final InitService initService;
	
	@PostConstruct
	public void init() {
		//@PostConstruct를 통해 init()가 실행될 때는 이미 Bean생성이 완료된 시점이다. 
		//@Transactional을 통해 DML 작업이 이루어지는 메소드를 Proxy로 감쌀 때는 IoC로 Bean을 생성하는 시점이다. 따라서 InitService Bean을 이용한다. 
		initService.dbInit1();
		initService.dbInit2();
	}
	
	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {
		
		private final EntityManager em;
		public void dbInit1() {
			Member member = createMember("userA", "서울", "서울로", "123-456");
			em.persist(member);
			
			Book book1 = createBook("JPA1 BOOK", 10000, 100);
			em.persist(book1);
			
			Book book2 = createBook("JPA2 BOOK", 20000, 100);
			em.persist(book2);
			
			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);
			
			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}
		
		public void dbInit2() {
			Member member = createMember("userB", "경기", "경기로", "777-456");
			em.persist(member);
			
			Book book1 = createBook("SPRING1 BOOK", 30000, 200);
			em.persist(book1);
			
			Book book2 = createBook("SPRING2 BOOK", 40000, 400);
			em.persist(book2);
			
			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 30000, 3);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
			
			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		private Delivery createDelivery(Member member) {
			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			return delivery;
		}
		
		private Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setName(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}
		
		private Book createBook(String name, int price, int StockQuantity) {
			Book book = new Book();
			book.setName(name);
			book.setPrice(price);
			book.setStockQuantity(StockQuantity);
			return book;
		}
	}
	
}
