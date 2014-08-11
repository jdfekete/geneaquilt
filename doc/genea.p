var v,minim:vertex;

procedure search(t:vertex);
{ breadth first search }
var queue,queuek:kazstack; v:vertex; rx:linesp;
begin
  queue:=nil; qq^.p^[t]:=maxvertex div 2;
  while t<>0 do begin
    rx:=prvi(q^.vl^[t],t,2);
    while rx<>nil do begin
      if rx^.i=t then v:=rx^.j else v:=rx^.i;
      if qq^.p^[v]=0 then begin {first seen}
        if rx^.edge then qq^.p^[v]:=qq^.p^[t]
        else if (rx^.smer and (rx^.i=t))or(not rx^.smer and (rx^.j=t)) then qq^.p^[v]:=qq^.p^[t]+1{round(rx^.val)}
        else qq^.p^[v]:=qq^.p^[t]-1{round(rx^.val)};
        if qq^.p^[v]<minim then minim:=qq^.p^[v];
        pushqueue(queue,queuek,v)
      end;
      rx:=naslednji(q^.vl^[t],rx,t,2)
    end;
    t:=popstack(queue);
  end;
end;

begin
  for v:=1 to q^.dimen do qq^.p^[v]:=0;
  minim:=maxvertex div 2;
  for v:=1 to q^.dimen do
    if qq^.p^[v]=0 then search(v);
  for v:=1 to q^.dimen do
    if qq^.p^[v]>0 then qq^.p^[v]:=qq^.p^[v]-minim+1;
end;