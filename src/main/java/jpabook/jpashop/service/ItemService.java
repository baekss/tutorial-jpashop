package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	@Transactional
	public void saveItem(Item item) {
		itemRepository.save(item);
	}
	
	//변경감지 방법으로 업데이트(권장방법). 수정이 필요한 필드만 업데이트 가능
	//merge는 merge를 통해 리턴된 영속객체와 변경 파라미터 객체의 상호간 All field 교환 매커니즘 이므로 
	//수정할 생각이 없던 field도 의도치 않게 default값(null, 0, false 등)으로 수정이 되어 비권장한다.
	@Transactional
	public void updateItem(Long itemId, int price, String name, int stockQuantity) {
		Item findItem = itemRepository.findOne(itemId);
		findItem.setPrice(price);
		findItem.setName(name);
		findItem.setStockQuantity(stockQuantity);
	}
	
	public List<Item> findItems() {
		return itemRepository.findAll();
	}
	
	public Item findOne(Long itemId) {
		return itemRepository.findOne(itemId);
	}
}
