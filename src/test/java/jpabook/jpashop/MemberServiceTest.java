package jpabook.jpashop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;

@RunWith(SpringRunner.class) //스프링과 함께 실행
@SpringBootTest //스프링 컨테이너 안에서 테스트 실행
@Transactional //Test코드에서는 기본적으로 rollback
public class MemberServiceTest {

	@Autowired 
	MemberService memberService;
	@Autowired 
	MemberRepository memberRepository;
	@Autowired
	EntityManager em;
	
	@Test
	//@Rollback(false) Test 코드에서 디폴트는 트랜잭션을 commit 하지 않는다. 
	public void 회원가입() throws Exception {
		//given
		Member member = new Member();
		member.setName("baek");
		
		//when
		Long saveId = memberService.join(member);
		
		//then
		em.flush(); //insert query 확인
		assertEquals(member, memberRepository.findOne(saveId));
	}
	
	@Test(expected=IllegalStateException.class)
	public void 중복_회원_예외() throws Exception {
		//given
		Member member = new Member();
		member.setName("baek");
		
		Member member2 = new Member();
		member2.setName("baek");
		
		//when
		memberService.join(member);
		memberService.join(member2);
		
		//then
		fail("예외가 발생해야 한다.");
	}

}
