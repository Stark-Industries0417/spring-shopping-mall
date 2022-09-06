package com.shop.domain.Item;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.domain.Item.QItem;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import com.shop.domain.Item.ItemRepository;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemRepositoryTest {

    @Autowired private ItemRepository itemRepository;
    @PersistenceContext EntityManager em;   // 영속성 컨텍스트 사용하기 위해 @PersistenceContext 어노테이션으로 EntityManager 빈 주입함

    @AfterEach
    public void cleanUp() {
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest() {
        Item saved = itemRepository.save(Item.builder()
                    .itemNm("테스트 상품")
                    .price(10000)
                    .itemDetail("테스트 상품 상세 설명")
                    .itemSellStatus(ItemSellStatus.SELL)
                    .stockNumber(100)
                    .build());
        System.out.println(saved.toString());
    }

    public void createItemList() {
        for(int i=1; i<=10; i++) {
            itemRepository.save(
                    Item.builder()
                            .itemNm("테스트 상품"+i)
                            .price(10000 + i)
                            .itemDetail("테스트 상품 상세 설명" + i)
                            .itemSellStatus(ItemSellStatus.SELL)
                            .stockNumber(100)
                            .build()
            );
        }
    }
    public void createItemList2() {
        for(int i=1; i <= 5; i++) {
            itemRepository.save(
                    Item.builder()
                            .itemNm("테스트 상품"+i)
                            .price(10000 + i)
                            .itemDetail("테스트 상품 상세 설명" + i)
                            .itemSellStatus(ItemSellStatus.SELL)
                            .stockNumber(100)
                            .build()
            );
        }
        // 품절
        for(int i =6; i <= 10; i++){
            itemRepository.save(
                    Item.builder()
                            .itemNm("테스트 상품"+i)
                            .price(10000 + i)
                            .itemDetail("테스트 상품 상세 설명" + i)
                            .itemSellStatus(ItemSellStatus.SOLD_OUT)
                            .stockNumber(0)
                            .build()
            );
        }
    }

    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNmTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemNm("테스트 상품1");
        for(Item item: itemList) {
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("상품명, 상품 상세설명 or 테스트")
    public void findByIdItemNmOrItemDetailTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemNmOrItemDetail("테스트 상품1", "테스트 상품 상세 설명5");
        for (Item item: itemList) {
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("가격 LessThan 테스트")
    public void findByPriceLessThanTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByPriceLessThan(10005);
        for(Item item: itemList) {
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("가격 내림차순 조회 테스트")
    public void findByPriceLessThanOrderByPriceDescTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByPriceLessThanOrderByPriceDesc(10005);
        for(Item item: itemList) {
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetail("테스트 상품 상세 설명");
        for(Item item: itemList) {
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("Querydsl 조회테스트1")
    public void queryDslTest() {
        this.createItemList();
        // JPAQueryFactory 이용하여 쿼리를 동적으로 생성, 생성자의 파라미터로는 EntityManager 객체 넣어줌
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        // Querydsl 통해 쿼리 생성하기 위해 플러그인 통해 자동으로 생성된 QItem 객체 이용
        QItem qItem = QItem.item;
        // 자바 소스이지만 SQL 문과 비슷하게 소스 작성 가능
        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%"+"테스트 상품 상세 설명"+"%"))
                .orderBy(qItem.price.desc());
        // JPAQuery 메서드중 하나인 fetch 이용해 쿼리 결과를 리스트로 반환
        // fetch() 메서드 실행 시점에 쿼리문 실행
        List<Item> itemList = query.fetch();

        for(Item item: itemList){
            System.out.println(item.toString());
        }
    }
    @Test
    @DisplayName("상품 Querydsl 조회 테스트 2")
    public void queryDslTest2() {
        this.createItemList2();
        // BooleanBuilder는 쿼리에 들어갈 조건을 만들어주는 빌더이다.
        // Predicate 을 구현하고 있으며 메소드 체인 형식으로 사용할 수 있음
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QItem item = QItem.item;

        String itemDetail = "테스트 상품 상세 설명";
        int price = 10003;
        String itemSellStat = "SELL";

        // 필요 상품 조회하는데 필요한 and 조건 추가하고 있음
        booleanBuilder.and(item.itemDetail.like("%" + itemDetail + "%"));
        booleanBuilder.and(item.price.gt(price));

        // 상품의 판매 상태가 SELL일 때만 booleanBuilder에 판매상태 조건을 동적으로 추가하는 것이다.
        if(StringUtils.equals(itemSellStat, ItemSellStatus.SELL)){
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        // 데이터 페이징해 조회하도록 PageRequest.of() 메서드 이용해 Pageable 객체 생성
        // 첫 번째 인자는 조회할 페이지 번호, 두 번쨰 인자는 한 페이지당 조회할 데이터 개수 넣어줌
        Pageable pageable = PageRequest.of(0, 5);

        // QueryDslPredicateExecutor 인터페이스에서 정의한 findAll() 메서드 이용해
        // 조건에 맞는 데이터를 Page 객체로 받아옴
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable);
        System.out.println("total elements : " + itemPagingResult.getTotalElements());

        List<Item> resultItemList = itemPagingResult.getContent();
        for(Item resultItem: resultItemList) {
            System.out.println(resultItem.toString());
        }
    }
}