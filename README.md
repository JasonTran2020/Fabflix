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
Demo video URL: 

Jason Tran's work:


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
* MovieListServlet
* MovieSearchServlet
* PurchaseServlet
* ShoppingCartServlet
* SingleMovieServlet
* SingleStarServlet