# Getting Started

## Para levantar la app en celular

1. Bajarse android studio de este [link](https://developer.android.com/studio?hl=es-419&gclid=Cj0KCQiAmL-ABhDFARIsAKywVacUNVs1HonwyqzHIZWnzVFka-tQGQ3dp3wMEyI_QOusjKRM4zEMyzgaAoJWEALw_wcB&gclsrc=aw.ds)
2. Dentro de android studio _File > New > Project from version control_ y agregan el repo: https://github.com/nicolasdeleon/Signal-Monitor-Android-App.git

    Con este paso ya pueden manipular/correr la app. Para que funcione bluetooth hay que correrla de un celular android, para esto primero hay que activar modo desarrollador en el celular.

3. Seguir los pasos de este video para ponerlo en modo desarrollador:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=gn4bRTFicZw
" target="_blank"><img src="http://img.youtube.com/vi/gn4bRTFicZw/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

4. En el celular asegurarse que en _Ajustes > Desarrollador > Depuración por USB_ esta activado
5. Volver a android studio y a la derecha del iconito de _Run 'app'_ les debería aparecer su celular, de ser así correr la app

## Para conectar la app con el HC-05

1. Con la app corriendo, prender bluetooth desde el botón de la app o desde _Ajustes_
2. El modulo HC-05 tiene que estar titilando rápido (modo esperando conexion)
3. Apretar en la app el boton Discover, para que aparezcan todos los dispositivos cercanos y esperar que aparezca el HC-05

    Acá pueden suceder 2 escenarios, que aparezca o que no aparezca.

4. APARECE) Hacer click en el HC-05 de la lista y apretar START CONNECTION, les podría pedir una clave _1234_ y después se debería conectar. El modulo HC-05 debería pasar a titilar menos seguido y con rafagas de 2. Ir de aca al paso 5
4. NO APARECE) Ir a bluetooth donde aparecen todos los dispositivos y intentar vincularse con el HC-05, debería solicitarles la clave _1234_ o directamente aparecerles que estan vinculados y no pasa mas nada. Una vez hecho esto, volver a la app y apretar directamente START CONNECTION. Esto debería establecer la conexión con el módulo y debería arrancar a titilar mas lento con rafagas de 2. Ir de aca al paso 5
5. Una vez conectado, recordar que el protocolo para enviar los datos es enviar mensajes del estilo "[DATA DEL CORAZON]-[DATA SP02]-[DATA DE LA TEMPERATURA]". De no coincidir con el formato, no se plotea nada. Los gráficos aparecen apenas empieza a recibir información la app