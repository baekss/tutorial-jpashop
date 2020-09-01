package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor //final 필드들로 구성된 생성자를 생성해 줌
public class MemberService {

	private final MemberRepository memberRepository;
	
	//@Autowired //class에 생성자가 하나 있을 땐 생성자에 명시를 안 해도 @Autowired가 암묵적으로 붙는다. 
	/* @RequiredArgsConstructor로 대체한다.
	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}
	*/
	
	@Transactional
	public Long join(Member member) {
		validateDuplicateMember(member);
		memberRepository.save(member);
		//영속성 컨텍스트에 올릴 때 member 객체에 id를 set하기 때문에 id 값을 얻을 수 있다.
		return member.getId();
	}

	private void validateDuplicateMember(Member member) {
		List<Member> findMembers = memberRepository.findByName(member.getName());
		if(!findMembers.isEmpty()){
			throw new IllegalStateException("이미 존재하는 회원입니다.");
		}
		
	}
	
	public List<Member> findMembers() {
		return memberRepository.findAll();
	}
	
	public Member findOne(Long memberId) {
		return memberRepository.findOne(memberId);
	}

	@Transactional
	public void update(Long id, String name) {
		Member member = memberRepository.findOne(id);
		member.setName(name);
	}
}
