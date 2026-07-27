-- Place your queries here. Docs available https://www.hugsql.org/
-- :name insert-slideshow :returning-execute
insert into slideshow (slideshow_name)
values (:slideshow_name)
returning slideshow_id;

-- :name insert-reading :returning-execute
insert into reading (reading_name)
values (:reading_name)
returning reading_id;

-- :name get-slideshows :query
select * from slideshow;
-- :name get-slideshows-flat :query
select slideshow_id, slideshow_name from slideshow;
-- :name get-readings :query
select * from reading;
-- :name get-slideshow :query :one
select * from slideshow where slideshow_id = :slideshow_id
-- :name get-reading :query :one
select * from reading where reading_id = :reading_id
-- :name get-slideshow-name :query :one
select * from slideshow where slideshow_name = :slideshow_name
-- :name get-reading-name :query :one
select * from reading where reading_name = :reading_name

-- :name slideshow-name :execute
update slideshow
set slideshow_name = :slideshow_name
where slideshow_id = :slideshow_id

-- :name slideshow-details :execute
update slideshow
set details = :details
where slideshow_id = :slideshow_id

-- :name slideshow-delete :execute
delete from slideshow where slideshow_id = :slideshow_id

-- :name reading-name :execute
update reading
set reading_name = :reading_name
where reading_id = :reading_id

-- :name reading-details :execute
update reading
set details = :details
where reading_id = :reading_id

-- :name reading-delete :execute
delete from reading where reading_id = :reading_id
