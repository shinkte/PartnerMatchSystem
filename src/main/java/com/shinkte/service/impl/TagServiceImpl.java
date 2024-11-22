package com.shinkte.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shinkte.model.domain.Tag;
import com.shinkte.service.TagService;
import com.shinkte.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author shinkte
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-06-30 16:35:13
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




