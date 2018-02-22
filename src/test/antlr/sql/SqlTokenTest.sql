-- 测试
create  table xxx as (
  -- 嵌套Select子句
  select a.*, #a.b.c+d.e,substr(b||'postfix', #start, #end),/*注
释*/'select '''', :a from t' from t )with test
;

INSERT INTO xxx(c1,c2,c3,c4)
values('1',2.4, '2017-01-01'),
    ('2',4,8,'2017-01-02')
;

select a.*
from (
  select c1,
    case c2 when 1 then '1' when 2 then '2' else '---' end,
    (case when c3=1 then '1' else nullif('', '') end) as c3,
    cast(c4 as char(4)) as c4
  from xxx
) a
where a.c1 > 100;