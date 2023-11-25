## CS 122B Project 

Yes the base of this is just the API example

---------
Project 1:
---------
Demo video URL: https://youtu.be/hW1ehcmigZw

  Jason Tran's work:
  * Created and completed movie-twenty-list html, javascript, and java servlet
  * Edited single-star html, javascript, and java servlet from api-example to meet project requirements
  * Added base CSS files and did CSS files specific to movie-twenty-list and single-star, such as color theming and shadows
  
  Eric Chang's work:
  * Created and completed single-movie html, javascript, and java servlet
  * Beautifed webpages by editing all CSS files and accounting for various punctuation cases
  * debugged problematic lines of code from movie-twenty-list and single-star html/js files 

---------
Project 2:
---------
Demo video URL: https://youtu.be/QAH5yKksrwk

Jason Tran's work:
* Implemented Browsing/Searching (Task 2)
* Extended Project1 functionality (Task 3)

Eric Chang's work:
* Implemented Login and Sessions(Task 1)
* Added Shopping Cart page and functionality (Task 4)

How the LIKE operator was used in searching:
* Used substring matching for EACH word in a parameter. Capitalization does not matter
* Example, if the parameter for title was "The terminal", the Like clause was (title LIKE %the% OR title LIKE %terminal%). 
* So any movies whose title had substring the or terminal would be included.
* AND is still used between different parameters, such as title and director.
* If title was "The terminal" and stars was "Tom Hanks", part of the WHERE clause would be (title LIKE "%the%" OR title LIKE %terminal%) 
 AND (starName LIKE "%tom%" OR starName LIKE "%hanks%")
* Went with this implementation as it yields the most amount of results while still being considered "relevant" (I mean the substring is in there)
* A person who wants to search movies (compared to say medical documents or court cases) probably won't be the most careful/precise when typing in their search query

---------
Project 3:
---------
Demo video URL: https://youtu.be/NlFyaPVZE-w

Jason Tran's work:
* Added HTTPS (Task 2)
* Encrypt passwords on AWS (Task 4)
* Did XML parsing and insertion (Task 6)


Eric Chang's work:
* Added reCAPTCHA (Task 1)
* Ensured consistent usage of PreparedStatements (Task 3)
* Implemented employee dashboard using stored procedures (Task 5)
* Encrypted the employee password (Task 4/5)

Files with PreparedStatements:


* EmployeeAddMovieServlet (Also uses CallableStatement)
* EmployeeAddStarServlet
* EmployeeLoginServlet
* EmployeeMetadataServlet
* LoginServlet
* datainserters.MovieInserter
* datainserters.StarInserter
* datainserters.StarInMovieInserter
* MovieListServlet
* MovieSearchServlet
* PurchaseServlet
* ShoppingCartServlet
* SingleMovieServlet
* SingleStarServlet

Inconsistency report when running locally
* Duplicate movies: 29
* No movie name: 7
* No movie year: 18
* No movie director name: 7
* No xml FID: 16
* Duplicate actors: 3
*  No actor name: 0
* No actor DOB: 2521
* Actors in cast but not actors file: 14808
* Duplicate cast: 1267
* Movie FIDs that could not be mapped: 948
* No movie FID for cast: 0
* No actor name for cast: 3

XML Parsing Optimizations (local testing time on Jason's PC, no auto-commit) <br>
Prior to any optimization, it took about 47,000 to 49,000 ms<br>
 <b>Multi-threading</b>
* Parsing of movies and actors takes place on separate threads (Parsing of casts waits for both of those to finish). 
* Reduced the time to around 41,000 to 43,000 ms<br>

<b>Batch updates</b>
* Added prepared statement in batches of size 100, then executed the batch update, and repeat until there was no more data
* Reduced time to around 44,000 ms to 45,000 ms<br>

<b>Both combined</b>
* Reduced time to around 39,000 ms to 40,000 ms <br>
* When auto-commit is on (I know it doesn't count as an optimization requirement), without the 2 optimizations it was about 7500ms, and with the 2 optimizations it was about 5100ms

---------
Project 4:
---------
Demo video URL: TODO

Jason Tran's work:
* Did fulltext and autocomplete on web (Task 1)



Eric Chang's work:
* Did all of the Android App (Task 2)
