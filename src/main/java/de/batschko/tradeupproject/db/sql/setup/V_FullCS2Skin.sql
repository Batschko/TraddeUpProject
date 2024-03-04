CREATE VIEW employee_view AS v_fullcs2skin
SELECT t3.id, t1.stash_id, t1.collection_id, t3.modified_date, t2.coll_name, t1.rarity, t3.stattrak, t1.weapon, t1.title, t3.`condition`, t1.float_start, t1.float_end, t1.image_url, t1.top, t3.skin_price_id, t4.price_type, t4.price, t4.amount_sold
FROM stash_skin_holder t1
JOIN collection t2 ON t1.collection_id = t2.id
JOIN c_s2_skin t3 ON t1.stash_id = t3.stash_id
JOIN skin_price t4 ON t3.skin_price_id = t4.id