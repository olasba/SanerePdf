# -*- coding: UTF-8 -*-

def rndItem(arr)
	arr[rand(arr.length)]
end

fnrInput = 
"06072626670
25047538788
03051335448
04050670738
09070739590
28086648034
14060602841
01019768291
10054838366
09050579600
24025514927
12073632321
05127913937
27061141273
01084049375
02090111380
09102749003
30048226694
17129849932
23013000700
10044205063
14059939905
04067702293
22017619156
07067547459
05059370091
05037808034
19029232310
28036220637
01058929189
22040213051
16111618902
18107645981
08063531309
06016004853
26124334747
04050141700
06112925161
14094923191
30016920238
17037902748
04058248997
09096724419
31129223734
18098031487
12044607873
02059882719
03119839105
30035637601
30093941698
19097106498
17101176851
16053227248
10101104090
13088833974
25115617810
08099335247
07079976592
25120087803
24045210347"

fnr = fnrInput.split("\n")
# p fnr

postnr = Hash.new
File.open("Postnummerregister_utf.txt", "r") do |file|
	file.each_line do |line|
		fields = line.split("\t")
		if fields.length>=4 then
			postnr[fields[0]] = fields[1]
			postnr[fields[2]] = fields[3]
		end
	end
end
# p postnr


forbr = ['obfuskering av kode', 'smugling av poteter', 'kobbertyverier', 'laber matlaging']
straff = ['oppsigelse av konto', 'senking av rente', 'fradrag i stemmeretten']

# puts rndItem(forbr) + " " + rndItem(straff)

37.times do |n|

frafrase = ["Fra: ", "Avsender: ", "Utsteder: "]
tilfrase = ["Til: ", "Mottaker: ", "Tiltredes: "]
fraseidx = rand(frafrase.length)

opprinnmal = '
\documentclass[a4paper, norwegian, 12pt]{article}
\usepackage[utf8]{inputenc}
\usepackage[T1]{fontenc}

\begin{document}
'
opprinnmal += frafrase[fraseidx]
opprinnmal += 'Øvrigheta

\vspace{0.42em}
Postboks 37B

\vspace{0.42em}
9475 Borkenes

\vspace{3em}
'

mal = opprinnmal

mal += tilfrase[fraseidx] + " Navn Q. Navnesen \n\n"
mal += 'Gateveien ' + rand(3000).to_s + "\n\n"
n0kla = postnr.keys
tilfnr = n0kla[rand(n0kla.length)]
mal += tilfnr + " " + postnr[tilfnr] + "\n\n"

pnr = rndItem(fnr)
personnr = pnr[0..5] + " " + pnr[6..pnr.length-1]


mal += '\vspace{3em}'
mal += "Personnr.: " + personnr + "\n\n"

mal += '\vspace{3em}'

mal += "I forbindelse med " + rndItem(forbr) + 
	" opplyses det om at De har medført " + rndItem(straff) + 
	".  Håper det smaker.\n"

mal += '\end{document}'

utfn = "fiktiv" + (n+2).to_s + ".tex"
File.open(utfn, "w") do |fh|
	fh.write(mal)
end

end

