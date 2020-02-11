for /r C:\Users\frase\Documents\test %%X in (*) do (
        echo %%X >> C:\Users\frase\Documents\out.txt
        echo -------------------------------------------------------------------- >> C:\Users\frase\Documents\out.txt
	echo. >> C:\Users\frase\Documents\out.txt
	type "%%X" >> C:\Users\frase\Documents\out.txt
        echo. >> C:\Users\frase\Documents\out.txt
        echo -------------------------------------------------------------------- >> C:\Users\frase\Documents\out.txt
)
