package project.trade;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookImgMapper {
    int save(@Param("imgUrl") String imgUrl, @Param("trade_seq") long trade_seq);
    List<String> findImgUrl(@Param("trade_seq") long trade_seq);

    int deleteBySeq(@Param("trade_seq") Long tradeSeq);
}
