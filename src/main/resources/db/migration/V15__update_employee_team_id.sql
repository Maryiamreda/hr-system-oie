update  employee  set employee.team_id=(
    select id from team where employee.team=name
    )
